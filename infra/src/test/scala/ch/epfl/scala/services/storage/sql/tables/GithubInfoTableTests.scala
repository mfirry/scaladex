package ch.epfl.scala.services.storage.sql.tables

import java.time.Instant

import ch.epfl.scala.index.Values
import ch.epfl.scala.services.storage.sql.BaseDatabaseSuite
import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers

class GithubInfoTableTests extends AsyncFunSpec with BaseDatabaseSuite with Matchers {
  import Values._

  import GithubInfoTable._
  describe("should generate the query for") {
    it("insert") {
      val q = insert(Scalafix.reference)(Scalafix.githubInfo)
      check(q)
      q.sql shouldBe
        s"""INSERT INTO github_info (organization, repository, name, owner,
           | homepage, description, logo, stars, forks, watchers, issues, readme,
           | contributors, contributorCount, commits, topics, contributingGuide,
           | codeOfConduct, chatroom, beginnerIssuesLabel, beginnerIssues,
           | selectedBeginnerIssues, updated_at) VALUES (?, ?, ?, ?,
           | ?, ?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""".stripMargin
          .filterNot(_ == '\n')
    }
    it("insertOrUpdate") {
      val q = insertOrUpdate(Scalafix.reference)(Scalafix.githubInfo, Instant.now())
      check(q)
      q.sql shouldBe
        s"""INSERT INTO github_info (organization, repository, name, owner,
           | homepage, description, logo, stars, forks, watchers, issues, readme,
           | contributors, contributorCount, commits, topics, contributingGuide,
           | codeOfConduct, chatroom, beginnerIssuesLabel, beginnerIssues,
           | selectedBeginnerIssues, updated_at) VALUES (?, ?, ?, ?,
           | ?, ?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
           | ON CONFLICT (organization, repository) DO UPDATE SET name=?, owner=?, homepage=?, description=?,
           | logo=?, stars=?, forks=?, watchers=?, issues=?, readme=?, contributors=?, contributorCount=?,
           | commits=?, topics=?, contributingGuide=?, codeOfConduct=?, chatroom=?, updated_at=?""".stripMargin
          .filterNot(_ == '\n')
    }
    it("selectAllTopics") {
      val q = selectAllTopics()
      check(q)
      q.sql shouldBe s"""SELECT topics FROM github_info where topics != ''"""
    }
  }

}
