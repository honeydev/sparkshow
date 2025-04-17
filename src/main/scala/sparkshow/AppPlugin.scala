package sparkshow

import cats.effect._
import distage.plugins.PluginDef
import doobie.util.transactor.Transactor
import izumi.distage.model.definition.ModuleDef
import izumi.distage.roles.model.definition.RoleModuleDef
import org.apache.spark.sql.SparkSession
import sparkshow.commands.{CreateUserTask, MigrateTask}
import sparkshow.conf.AppConf
import sparkshow.db.PGTransactorResource
import sparkshow.db.repositories.{QueryRepository, RoleRepository, SourceRepository, UserRepository}
import sparkshow.services.{AuthService, QueryService, SourceService, UserService}
import sparkshow.tasks.RunQueriesTask
import sparkshow.utils.SparkSessionResource
import sparkshow.web.routes.{AuthRoutes, JWTMiddleware, QueryRoutes, RoutesFacade, SourceRoutes}

object AppPlugin extends PluginDef {
    include(modules.roles)
    include(modules.conf)
    include(modules.web)

    object modules {

        def roles: RoleModuleDef = new RoleModuleDef {
            makeRole[CreateUserTask]
            makeRole[MigrateTask]
        }

        def conf: ModuleDef = new ModuleDef {
            make[AppConf].from(AppConf.load)
        }

        def web: RoleModuleDef = new RoleModuleDef {
            make[Transactor[IO]].fromResource[PGTransactorResource]
            make[SparkSession].fromResource[SparkSessionResource]
            make[UserRepository]
            make[RoleRepository]
            make[QueryRepository]
            make[SourceRepository]
            make[SourceService]
            make[AuthService]
            make[QueryService]
            make[AuthRoutes]
            make[UserService]
            make[QueryRoutes]
            make[SourceRoutes]
            make[RoutesFacade]
            make[JWTMiddleware]
            make[RunQueriesTask]
            make[HttpServer].fromResource[HttpServer.Impl]
            makeRole[AppServiceRole]
        }
    }
}
