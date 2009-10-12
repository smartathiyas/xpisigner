package org.oregan.xpi;

public class XPIException extends Throwable
{
    private int err;

    public XPIException(String message, int errCode)
    {
        super(message);
        this.err = errCode;
    }

    public int getErr()
    {
        return err;
    }
}
