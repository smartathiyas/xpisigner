package org.oregan.gui.capi;

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
