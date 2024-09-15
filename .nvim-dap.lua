local dap = require("dap")

dap.configurations.scala = {
  {
    type = "scala",
    request = "launch",
    name = "web",
    metals = {
      runType = "runOrTestFile",
      mainClass = "sparkshow.SparkshowLauncher",
      args = { ":web" }, -- here just as an example
    },
  },
  {
    type = "scala",
    request = "launch",
    name = "create-user",
    metals = {
      runType = "runOrTestFile",
      mainClass = "sparkshow.SparkshowLauncher",
      args = {
          ":create-user -cli-command true --username test",
          "-cli-command",
          "true",
          "--username",
          "test",
          "--password test",
          "--email test@email.com",
          "--roles ADMIN"
        }, -- here just as an example
    }
  },
  {
    type = "scala",
    request = "launch",
    metals = {
      runType = "testTarget",
    },
  },
  {
    type = "scala",
    request = "launch",
    name = "play",
    buildTarget = "play-test-build",
    metals = {
      mainClass = "app.Server",
      buildTarget = "play-test-build",
      runType = "run",
      args = {},
      jvmOptions = {"-Dconfig.file=/path/to/production.conf"}
    }
  }
}

