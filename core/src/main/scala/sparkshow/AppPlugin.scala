package sparkshow

import cats.effect._
import distage.plugins.PluginDef
import doobie.util.transactor.Transactor
import izumi.distage.model.definition.ModuleDef
import izumi.distage.roles.model.definition.RoleModuleDef
import sparkshow.commands.CreateUserTask
import sparkshow.commands.MigrateTask
import sparkshow.conf.AppConf
import sparkshow.db.PGTransactorResource
import sparkshow.db.repositories.MetricRepository
import sparkshow.db.repositories.QueryRepository
import sparkshow.db.repositories.RoleRepository
import sparkshow.db.repositories.SourceRepository
import sparkshow.db.repositories.UserRepository
import sparkshow.services.AuthService
import sparkshow.services.LocalSparkMetricCalcService
import sparkshow.services.MetricService
import sparkshow.services.QueryQueueService
import sparkshow.services.QueryService
import sparkshow.services.SourceService
import sparkshow.services.UserService
import sparkshow.tasks.RunQueriesTask
import sparkshow.web.routes.AuthRoutes
import sparkshow.web.routes.JWTMiddleware
import sparkshow.web.routes.QueryRoutes
import sparkshow.web.routes.RoutesFacade
import sparkshow.web.routes.SourceRoutes
import sparkshow.web.routes.WSRoutes

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
            make[UserRepository]
            make[RoleRepository]
            make[QueryRepository]
            make[SourceRepository]
            make[MetricRepository]
            make[SourceService]
            make[AuthService]
            make[QueryService]
            make[MetricService]
            make[AuthRoutes]
            make[UserService]
            make[QueryRoutes]
            make[SourceRoutes]
            make[RoutesFacade]
            make[JWTMiddleware]
            make[RunQueriesTask]
            make[QueryQueueService]
            make[WSRoutes]
            make[LocalSparkMetricCalcService]
            make[HttpServer].fromResource[HttpServer.Impl]
            makeRole[AppServiceRole]
        }
    }
}
