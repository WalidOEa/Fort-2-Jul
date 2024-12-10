import subprocess
import sys


def install(package):
    subprocess.check_call([sys.executable, "-m", "pip", "install", package])


def install_fortran_linter():
    install("fortran-linter")


def verify_install():
    try:
        import fortran_linter
        print("Fortran Linter installed successfully")
    except ImportError:
        print("Fortran Linter not installed")


if __name__ == '__main__':
    install_fortran_linter()
    verify_install()
