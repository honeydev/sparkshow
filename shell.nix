{ pkgs ? import <nixpkgs> {} }:
let
  stdenv = pkgs.llvmPackages_15.stdenv; in rec {
  project = stdenv.mkDerivation {
    name = "sparkshow";

#    nativeBuildInputs = [
#      pkgs.cmake
#      pkgs.ninja
#    ];

    buildInputs = [
      pkgs.jdk21
      # pkgs.sbt
      pkgs.coursier
      pkgs.metals
      pkgs.postgresql
    ];

    shellHook = ''
      export PATH="$PATH:/home/honey/.local/share/coursier/bin"
      # fix https://stackoverflow.com/questions/73465937/apache-spark-3-3-0-breaks-on-java-17-with-cannot-access-class-sun-nio-ch-direct
      export JDK_JAVA_OPTIONS='--add-opens=java.base/sun.nio.ch=ALL-UNNAMED'
  '';
  };
}
