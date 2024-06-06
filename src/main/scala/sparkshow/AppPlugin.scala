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
import sparkshow.db.repository.RoleRepository
import sparkshow.db.repository.UserRepository
import sparkshow.service.AuthService
import sparkshow.service.UserService
import sparkshow.web.routes.AuthRoutes
import sparkshow.web.routes.QueryRoutes
import sparkshow.web.routes.RoutesFacade

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
            make[AuthService]
            make[AuthRoutes]
            make[UserService]
            make[QueryRoutes]
            make[RoutesFacade]
            make[HttpServer].fromResource[HttpServer.Impl]
            makeRole[AppServiceRole]
        }
    }
}
