{ stdenv, lib, fetchgit, buildGoPackage, makeWrapper, xcodeenv, androidenv }:

{ xcodeWrapperArgs ? { }
, xcodeWrapper ? xcodeenv.composeXcodeWrapper xcodeWrapperArgs
, androidPkgs ? androidenv.androidPkgs_9_0.androidsdk }:

let
  inherit (stdenv) isDarwin;
  inherit (lib) optional strings;
in buildGoPackage rec {
  pname = "gomobile";
  version = "20200622-${strings.substring 0 7 src.rev}";

  src = fetchgit {
    # WARNING: Next commit removes support for ARM 32 bit builds for iOS
    rev = "33b80540585f2b31e503da24d6b2a02de3c53ff5";
    name = "gomobile";
    url = "https://go.googlesource.com/mobile";
    sha256 = "0c9map2vrv34wmaycsv71k4day3b0z5p16yzxmlp8amvqb38zwlm";
  };

  goPackagePath = "golang.org/x/mobile";
  subPackages = [ "bind" "cmd/gobind" "cmd/gomobile" ];
  goDeps = ./deps.nix;

  patches = [ ./resolve-nix-android-sdk.patch ];

  buildInputs = [ makeWrapper ] ++ optional isDarwin xcodeWrapper;

  # Prevent a non-deterministic temporary directory from polluting the resulting object files
  postPatch = ''
    substituteInPlace cmd/gomobile/env.go \
      --replace \
        'tmpdir, err = ioutil.TempDir("", "gomobile-work-")' \
        "tmpdir = filepath.Join(os.Getenv(\"NIX_BUILD_TOP\"), \"gomobile-work\")" \
      --replace '"io/ioutil"' ""
    substituteInPlace cmd/gomobile/init.go \
      --replace \
        'tmpdir, err = ioutil.TempDir(gomobilepath, "work-")' \
        "tmpdir = filepath.Join(os.Getenv(\"NIX_BUILD_TOP\"), \"work\")"
  '';

  preBuild = ''
    mkdir $NIX_BUILD_TOP/gomobile-work $NIX_BUILD_TOP/work
  '';

  # The source needs to be available in the GOPATH when using gomobile.
  postInstall = ''
    mkdir -p $(dirname $out/src/$goPackagePath)
    ln -s $src $out/src/$goPackagePath
    wrapProgram $out/bin/gomobile \
      --set ANDROID_HOME "${androidPkgs}/libexec/android-sdk" \
      --prefix PATH : "${androidPkgs}/bin" \
      --prefix GOPATH : "$out"
  '';

  meta = with lib; {
    description = "A tool for building and running mobile apps written in Go";
    longDescription = "Gomobile is a tool for building and running mobile apps written in Go.";
    homepage = "https://pkg.go.dev/golang.org/x/mobile/cmd/gomobile";
    license = licenses.bsd3;
    maintainers = with maintainers; [ jakubgs ];
    platforms = with platforms; linux ++ darwin;
  };
}
