package sparkshow

import cats.effect._
import distage.plugins.PluginDef
import doobie.hikari.HikariTransactor
import izumi.distage.roles.model.{RoleDescriptor, RoleTask}
import izumi.distage.roles.model.definition.RoleModuleDef
import izumi.fundamentals.platform.cli.model.raw.RawEntrypointParams
import sparkshow.conf.AppConf
import sparkshow.db.PGTransactorResource
import sparkshow.db.repository.UserRepository
import sparkshow.service.AuthService
import sparkshow.commands.CreateUserTask
import izumi.distage.model.definition.ModuleDef

object AppPlugin extends PluginDef {
  include(modules.roles)
  include(modules.conf)

  //def roleModule = new RoleModuleDef {
  //  make[AppConf].from(AppConf.load)
  //  make[HikariTransactor[IO]].fromResource[PGTransactorResource]
  //  make[UserRepository]
  //  make[AuthService]
  //}

  object modules {
    
    def roles: RoleModuleDef = new RoleModuleDef {
       makeRole[CreateUserTask]
    }

    def conf: ModuleDef = new ModuleDef {
      make[AppConf].from(AppConf.load)
    }
  }
}
