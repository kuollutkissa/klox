{
  description = "A very basic flake";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs?ref=nixos-unstable";
  };

  outputs = { self, nixpkgs }: {
    devShells.x86_64-linux.default = let
      pkgs = import nixpkgs { system = "x86_64-linux";};
      jdk = pkgs.temurin-bin-21;
      in pkgs.mkShell {
        buildInputs = with pkgs; [
        jdk
        (callPackage gradle-packages.gradle_8 {
          java = jdk;
        })
        ];
        shellHook = ''
        echo "KLox"
        idea-community
        '';
      };
  };
}
