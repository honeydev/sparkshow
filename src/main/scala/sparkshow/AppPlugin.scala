package sparkshow

import cats.effect._
import distage.plugins.PluginDef
import doobie.hikari.HikariTransactor
import izumi.distage.model.definition.ModuleDef
import izumi.distage.roles.model.definition.RoleModuleDef
import sparkshow.commands.CreateUserTask
import sparkshow.conf.AppConf
import sparkshow.db.PGTransactorResource
import sparkshow.db.repository.UserRepository
import sparkshow.service.AuthService
import sparkshow.web.routes.{AuthRoutes, QueryRoutes, RoutesFacade}

object AppPlugin extends PluginDef {
  include(modules.roles)
  include(modules.conf)
    include(modules.web)



  object modules {
    
    def roles: RoleModuleDef = new RoleModuleDef {

        makeRole[CreateUserTask]
    }

    def conf: ModuleDef = new ModuleDef {
      make[AppConf].from(AppConf.load)
    }

      def web: RoleModuleDef = new RoleModuleDef {

          make[HikariTransactor[IO]].fromResource[PGTransactorResource]
          make[UserRepository]
          make[AuthService]
          make[AuthRoutes]
          make[QueryRoutes]
          make[RoutesFacade]
          make[HttpServer].fromResource[HttpServer.Impl]
          makeRole[AppServiceRole]
      }
  }
}
