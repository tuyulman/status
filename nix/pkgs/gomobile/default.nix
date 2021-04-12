{ stdenv, lib, fetchgit, buildGoModule, zlib
, makeWrapper, xcodeWrapper, androidPkgs }:

buildGoModule {
  pname = "gomobile";
  version = "2021-02-20";

  vendorSha256 = "1n1338vqkc1n8cy94501n7jn3qbr28q9d9zxnq2b4rxsqjfc9l94";

  src = fetchgit {
    # WARNING: Next commit removes support for ARM 32 bit builds for iOS
    rev = "bdb1ca9a1e083af5929a8214e8a056d638ebbf2d";
    name = "gomobile";
    url = "https://go.googlesource.com/mobile";
    sha256 = "0c4x6ki0fd3ka8z9bgw3v6ij6yrwb5ic2rx8pcp1k9fp4hx3d9fg";
  };

  subPackages = [ "bind" "cmd/gobind" "cmd/gomobile" ];

  # Fails with: go: cannot find GOROOT directory
  doCheck = false;

  patches = [ ./resolve-nix-android-sdk.patch ];

  nativeBuildInputs = [ makeWrapper ]
    ++ lib.optional stdenv.isDarwin xcodeWrapper;

  # Prevent a non-deterministic temporary directory from polluting the resulting object files
  postPatch = ''
    substituteInPlace cmd/gomobile/env.go --replace \
      'tmpdir, err = ioutil.TempDir("", "gomobile-work-")' \
      'tmpdir = filepath.Join(os.Getenv("NIX_BUILD_TOP"), "gomobile-work")' \
      --replace '"io/ioutil"' ""
    substituteInPlace cmd/gomobile/init.go --replace \
      'tmpdir, err = ioutil.TempDir(gomobilepath, "work-")' \
      'tmpdir = filepath.Join(os.Getenv("NIX_BUILD_TOP"), "work")'
  '';

  # Necessary for GOPATH when using gomobile.
  postInstall = ''
    mkdir -p $out/src/golang.org/x
    ln -s $src $out/src/golang.org/x/mobile
    wrapProgram $out/bin/gomobile \
      --prefix LD_LIBRARY_PATH : "${lib.makeLibraryPath [ zlib ]}" \
      --prefix PATH : "${androidPkgs}/bin" \
      --set ANDROID_HOME "${androidPkgs}"
  '';

  meta = with lib; {
    description = "A tool for building and running mobile apps written in Go";
    homepage = "https://pkg.go.dev/golang.org/x/mobile/cmd/gomobile";
    license = licenses.bsd3;
    maintainers = with maintainers; [ jakubgs ];
  };
}
