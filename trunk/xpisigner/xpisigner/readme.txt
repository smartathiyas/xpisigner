=========================
XPISigner - Version: 2.01
=========================

Copyright (c) Kevin O'Regan 2009

Installation
============

o Unzip to a temporary directory
o If java is not on your path Edit xpisiger.cmd or xpisigner.sh to point to your java installation.

Note: Java5 is required for XPISigner to run correctly.

On Unix:
  chmod o+x xpisigner.sh

Running XPISigner
=================

xpisigner.sh pfxfile password <baseDir|listing.txt> output.xpi
or
xpisigner.cmd pfxfile password <baseDir|listing.txt> output.xpi

    keystore.pfx    The PFX/PKCS#12 file containing your signing credentials.
    password        The passphrase for pfxfile.
    basedir         Include all files under basedir.
    listing.txt     Include only the files found in listfile. The files are
                    assumed to be located relative to the current directory.
    output.xpi      Filename for the signed xpi file.


Help & Support
==============

Contact me via the comments on the download page:

http://o-regan.org/xpisigner-secure-your-firefox-extensions/download-xpisigner/

If xpisigner has saved you time and effort, please consider donating.

Thank you for supporting XPISigner

kevin