package scaladex.server.service

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration._

import ch.epfl.scala.index.newModel.NewProject
import ch.epfl.scala.search.ProjectDocument
import ch.epfl.scala.services.SchedulerDatabase
import ch.epfl.scala.services.SearchEngine
import com.typesafe.scalalogging.LazyLogging
import ch.epfl.scala.utils.ScalaExtensions._

class SearchSynchronizer(db: SchedulerDatabase, searchEngine: SearchEngine)(implicit ec: ExecutionContext)
    extends Scheduler("search-synchronizer", 30.minutes)
    with LazyLogging {
  override def run(): Future[Unit] =
    db.getAllProjects().flatMap { projects =>
      logger.info(s"Syncing search of ${projects.size} projects")
      projects.mapSync(insertDocument).map(_ => ())
    }

  private def insertDocument(project: NewProject): Future[Unit] =
    for {
      releases <- db.findReleases(project.reference)
      inverseProjectDependencies <- db.countInverseProjectDependencies(project.reference)
      document = ProjectDocument(project, releases, inverseProjectDependencies)
      _ <- searchEngine.insert(document)
    } yield ()
}
