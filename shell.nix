{ pkgs ? import <nixpkgs> {} }:
let
  stdenv = pkgs.llvmPackages_15.stdenv;
in rec {
  project = stdenv.mkDerivation {
    name = "sparkshow";

#    nativeBuildInputs = [
#      pkgs.cmake
#      pkgs.ninja
#    ];

    buildInputs = [
      pkgs.jdk8
      # pkgs.sbt
      pkgs.coursier
      pkgs.postgresql
    ];

    shellHook = ''
      export PATH="$PATH:/home/honey/.local/share/coursier/bin"
  '';

  };
}
