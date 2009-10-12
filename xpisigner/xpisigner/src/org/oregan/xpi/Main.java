package org.oregan.xpi;

import java.io.*;
import java.lang.reflect.Constructor;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;

public class Main
{
    public static final int ERR_INCORRECT_PARAMS = 100;
    public static final int ERR_FILE_NOT_FOUND = 101;
    public static final int ERR_EMPTY_FILE_LIST = 102;
    public static final int ERR_INVALID_MODE = 103;
    public static final int ERR_MODE_FAILURE = 104;
    public static final int ERR_NO_DIGEST_ALGS = 110;
    public static final int ERR_ERROR_READING_FILE = 111;
    public static final int ERR_KEY_LOAD = 120;
    public static final int ERR_BAD_PASSWORD = 121;
    public static final int ERR_NO_SIGNING_KEY = 122;
    public static final int ERR_KEY_CERT_MISMATCH = 123;
    public static final int ERR_SIGNING_FAILED = 124;
    public static final int ERR_AUTO_FIND_FAILED = 125;


    private static final String VERSION = "2.0";

    private static final String INTRODUCTION = "XPISigner v" + VERSION + " (http://o-regan.org/xpisigner-secure-your-firefox-extensions)\n" +
        "Copyright 2009 - Kevin O'Regan";
    private static Boolean verbose = true;

    public static void main(String[] args) throws IOException
    {
        if (args.length != 4)
        {
            usage();
            System.exit(ERR_INCORRECT_PARAMS);
        }

//        if(!Integrity.selfIntegrityChecking())
//        {
//            System.out.println("This installation has been tampered with. Contact support@o-regan.org for help.");
//            System.exit(0);
//        }


        System.out.println(INTRODUCTION);
        System.out.println();
        verbose = Boolean.valueOf(System.getProperty("xpi.verbose", "false"));

        String location = args[0];
        String password = args[1];
        String listingFile = args[2];
        String out = args[3];

        ArrayList<String> listing;

        location = deQuote(location);
        password = deQuote(password);
        listingFile = deQuote(listingFile);
        out = deQuote(out);


        File listFile = new File(listingFile);


        if (verbose)
            System.out.println(listFile.getAbsoluteFile());
        if (!listFile.exists())
        {
            logMessage("File or directory \'" + listFile + "\' does not exist.");
            System.exit(ERR_FILE_NOT_FOUND);
        }
        String baseDir = "";
        if (listFile.isFile())
        {
            listing = parseListingFile(listFile);
            baseDir = listFile.getParent();
        } else
        {
            listing = generateListing(listFile, out);
            baseDir = listFile.getPath();
        }
        if (listing.size() == 0)
        {
            logMessage("Listing contains no files or folders. Nothing to do.");
            System.exit(ERR_EMPTY_FILE_LIST);
        }

        doSign(location, password, baseDir, listing, out);

        System.out.println("Done.");
        System.exit(0);

    }

    private static String deQuote(String arg) {
        if(arg.startsWith("\"") && arg.endsWith("\""))
        {
            arg = arg.substring(1, arg.length()-1);
        }
        return arg;
    }

    private static void usage()
    {
        String mode = System.getProperty("xpi.mode", "bc").toLowerCase();
        if (mode.equals("bc"))
        {
            System.out.println("XPISigner v" + VERSION + "\n" +
                "Archives the specified files and signs the archive using your digital id. \n" +
                "\n" +
                "Signed archives are compatible with Firefox and Thunderbird \n" +
                "\n" +
                "xpisigner keystore.pfx password basedir output.xpi \n" +
                "xpisigner keystore.pfx password listing.txt output.xpi \n" +
                "\n" +
                " keystore.pfx      The PFX/PKCS#12 file containing your signing credentials.\n" +
                " password          The passphrase for pfxfile.\n" +
                " basedir           Include all files under basedir.\n" +
                " listfile          Include only the files found in listfile. The files are assumed\n" +
                "                   to be located relative to the current directory.\n" +
                " output.xpi        Filename for the signed xpi file.\n" +
                "\n" +
                "Any signature files in META-INF/ will be overwritten.\n" +
                "\n" +
                "Copyright 2009 - Kevin O'Regan (http://o-regan.org)");
        }else if (mode.equals("lunasa"))
        {
                  System.out.println("XPISigner v" + VERSION + "\n" +
                "Archives the specified files and signs the archive using your digital id. \n" +
                "\n" +
                "Signed archives are compatible with Firefox and Thunderbird \n" +
                "\n" +
                "xpisigner alias password basedir output.xpi \n" +
                "xpisigner alias password listing.txt output.xpi \n" +
                "\n" +
" alias             The alias for the private key on the LunaSA.\n" +
" password          The passphrase for the device.\n" +
" basedir           Include all files under basedir.\n" +
" listfile          Include only the files found in listfile. The files are assumed\n" +
"                   to be located relative to the current directory.\n" +
" output.xpi        Filename for the signed xpi file.\n" +
                "\n" +
                "Any signature files in META-INF/ will be overwritten.\n" +
                "\n" +
                "Copyright 2009 - Kevin O'Regan (http://o-regan.org)");

        }
        else
        {
            System.out.println("XPISigner v" + VERSION + "\n" +
                "Archives the specified files and signs the archive using your digital id. \n" +
                "\n" +
                "Signed archives are compatible with Firefox and Thunderbird \n" +
                "\n" +
                "xpisigner firefox_profile_dir password basedir output.xpi \n" +
                "xpisigner firefox_profile_dir password listing.txt output.xpi \n" +
                "\n" +
                " firefox_profile_dir  Your firefox profile directory.\n" +
                "                      e.g. ~/.mozilla/firefox/6k9i1ild.default\n" +
                " password             The passphrase for the firefox cert store.\n" +
                " basedir              Include all files under basedir.\n" +
                " listfile             Include only the files found in listfile. The files are assumed\n" +
                "                      to be located relative to the current directory.\n" +
                " output.xpi           Filename for the signed xpi file.\n" +
                "\n" +
                "Any signature files in META-INF/ will be overwritten.\n" +
                "\n" +
                "Copyright 2009 - Kevin O'Regan (http://o-regan.org)");
        }
    }

    private static XPISigner getInstance(String pfxfile, String password, String baseDir, ArrayList listing, String out)
    {
        try
        {
            String mode = System.getProperty("xpi.mode", "bc").toLowerCase();
            boolean valid = (mode.equalsIgnoreCase("jss")
                || mode.equalsIgnoreCase("bc")
                || mode.equalsIgnoreCase("mscapi")
                || mode.equalsIgnoreCase("lunasa")
            );
            if (!valid)
            {
                logMessage("Invalid mode of operation " + mode);
                System.exit(ERR_INVALID_MODE);
            }
            Class klass = Class.forName("org.oregan.xpi." + mode + ".XPISignerImpl");
            Constructor c = klass.getConstructor(String.class, String.class, String.class, ArrayList.class, String.class);
            return (XPISigner) c.newInstance(pfxfile, password, baseDir, listing, out);
        } catch (Exception e)
        {
            e.printStackTrace();
            logMessage("Failed to load XPISigner internal module: Check xpi.jar is intact.");
            System.exit(ERR_MODE_FAILURE);
            return null;
        }
    }

    private static void doSign(String pfxfile, String password, String baseDir, ArrayList listing, String out)
        throws IOException
    {
        XPISigner xpiSigner = getInstance(pfxfile, password, baseDir, listing, out);

        if (xpiSigner == null)
        {
            System.out.println("Failed to load signing engine");
            System.exit(3);
        }

        XPIInfo info = null;
        try
        {
            info = xpiSigner.generateXPI();
        } catch (Exception e)
        {
            e.printStackTrace();
        } catch (XPIException e)
        {
            if (verbose)
                e.printStackTrace();
            else
                System.out.println(e.getMessage());
            System.exit(e.getErr());
        }

        if (info == null)
        {
            System.out.println("Signing engine failed to return status report.");
            System.exit(3);
        }

        File outputFile = info.getFile();

        String signer = info.getSignerDN();
        if (signer == null)
        {
            System.out.println("Warning: Status report contains no signer information");
            signer = "<none>";
        }
        int numEntries = info.getNumEntries();
        long lastModified = outputFile.lastModified();

        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setGroupingUsed(true);

        System.out.print("Generated XPI...\n" +
            "\tFilename:    " + outputFile.getCanonicalPath() + "\n" +
            "\tSize:        " + nf.format(outputFile.length() / 1024) + " kb\n" +
            "\tCreated at:  " + new Date(lastModified) + "\n" +
            "\tSigned by:   " + signer + "\n" +
            "\tNum entries: " + numEntries + "\n");

        System.out.println("Done.");
        System.exit(0);
    }

    public static ArrayList<String> parseListingFile(File listFile)
    {
        if (verbose)
            System.out.println("Parsing listing file: " + listFile);
        try
        {
            LineNumberReader br = new LineNumberReader(new BufferedReader(new FileReader(listFile)));
            ArrayList<String> pathList = new ArrayList<String>();
            String line;
            while ((line = br.readLine()) != null)
            {
                line = line.replace('\\', '/');
                pathList.add(line);
            }
            if (verbose)
            {
                System.out.println(pathList);
            }
            return pathList;
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<String> generateListing(File basedir, String out)
    {
        try
        {
            if (verbose)
                logMessage("Generating listing from directory: " + basedir);

            ArrayList<File> fileList = new ArrayList<File>();
            ArrayList<String> removalList = new ArrayList<String>();

            File c14nBaseDir = new File(basedir.getAbsoluteFile().getCanonicalPath());
            String c14nBasePath = c14nBaseDir.getPath();
            int basePathLength = c14nBasePath.length() + 1;

            File outFile = new File(out);
            outFile = outFile.getAbsoluteFile().getCanonicalFile();
            String outParent = outFile.getParent();
//            logMessage("outParent    = " + outParent);
//            logMessage("c14nBasePath = " + c14nBasePath);

            if (outParent.length() >= c14nBasePath.length())
            {
                if (outParent.compareTo(c14nBasePath) == 0)
                {
                    logMessage("Output under base dir, adding output file to exclusion list");
                    String s = outFile.getPath();
                    s = s.substring(basePathLength);
                    s = s.replace('\\', '/');
                    removalList.add(s);
                }

            }


            addFolder(c14nBaseDir, fileList);

            ArrayList<String> filePaths = new ArrayList<String>(fileList.size());
            for (File file : fileList)
            {
                String s = file.getPath();
                s = s.substring(basePathLength);
                s = s.replace('\\', '/');
                filePaths.add(s);
            }
            if (verbose) logMessage("Intermediate file list :" + filePaths);
            for (String path : filePaths)
            {
                if (path.startsWith("META-INF"))
                {
                    removalList.add(path);
                }
            }
            logMessage("Excluding: " + removalList);
            for (String path : removalList)
            {
                filePaths.remove(path);
            }
            if (verbose) logMessage("Final file list:" + filePaths);
            return filePaths;
        } catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static void addFolder(File folderName, ArrayList<File> fileList)
    {
        if (verbose) logMessage("Inspecting folder: " + folderName);
        File[] files = folderName.listFiles();
        for (File file : files)
        {
            if (file.isDirectory())
            {
                addFolder(file, fileList);
            } else
            {
                if (verbose) logMessage("\tAdding file: " + file);
                fileList.add(file);
            }
        }
    }

    public static void logMessage(String message)
    {
        try
        {
            String file = System.getProperty("xpi.log.file", "");
            if(file.length()>0)
        {
            File filename = new File(file);
                FileOutputStream fios = new FileOutputStream(filename,true);
                fios.write((message+"\n").getBytes());
                fios.close();

            }else
            {
                System.out.println(message);
            }
        } catch (IOException e)
        {
            System.out.println("Failed to write logfile entry:\n" + e);
            System.out.println(message);
        }

    }


}

