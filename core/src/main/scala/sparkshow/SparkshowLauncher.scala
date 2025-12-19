package sparkshow

import cats.effect.IO
import distage.plugins.PluginConfig
import izumi.distage.roles.RoleAppMain

object SparkshowLauncher extends RoleAppMain.LauncherCats[IO] {
    override def pluginConfig: PluginConfig = {
        PluginConfig.const(
          // add the plugin with ExampleRoleTask
          AppPlugin
        )
    }
}
