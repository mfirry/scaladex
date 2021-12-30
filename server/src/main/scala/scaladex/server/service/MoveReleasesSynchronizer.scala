package scaladex.server.service

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration._

import scaladex.core.model.GithubStatus
import scaladex.core.service.SchedulerDatabase
import scaladex.core.util.ScalaExtensions._

class MoveReleasesSynchronizer(db: SchedulerDatabase)(implicit ec: ExecutionContext)
    extends Scheduler("move-releases", 5.minutes) {
  override def run(): Future[Unit] =
    for {
      projectStatuses <- db.getAllProjectStatuses()
      moved = projectStatuses.collect { case (ref, GithubStatus.Moved(_, newRef)) => ref -> newRef }.toMap
      numberOfUpdated <- moved.map {
        case (oldRef, newRef) => db.findReleases(oldRef).flatMap(releases => db.updateReleases(releases, newRef))
      }.sequence
      _ = logger.info(
        s"${numberOfUpdated.sum} releases have been updated with the new organization/new repository names"
      )
    } yield ()

}