{
  description = "Scala project dev shell with sbt and JDK";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
  };

  outputs = { self, nixpkgs }:
    let
      systems = [ "x86_64-linux" "aarch64-linux" "x86_64-darwin" "aarch64-darwin" ];
      forAllSystems = f:
        nixpkgs.lib.genAttrs systems (system:
          f {
            pkgs = import nixpkgs { inherit system; };
          });
    in
    {
      devShells = forAllSystems ({ pkgs }: {
        default = pkgs.mkShell {
          packages = with pkgs; [
            jdk21
            metals
            sbt
            scala
            postgresql
          ];

          shellHook = ''
            echo "Scala dev shell"
            echo "Java:  $(java -version 2>&1 | head -n 1)"
            echo "sbt:   $(sbt --script-version)"
            echo "Scala: $(scala -version 2>&1 | head -n 1)"
          '';
        };
      });
    };
}
