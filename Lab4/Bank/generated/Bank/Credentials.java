//
// Copyright (c) ZeroC, Inc. All rights reserved.
//
//
// Ice version 3.7.2
//
// <auto-generated>
//
// Generated from file `Bank.ice'
//
// Warning: do not edit this file.
//
// </auto-generated>
//

package Bank;

public class Credentials implements java.lang.Cloneable,
                                    java.io.Serializable
{
    public String pesel;

    public String password;

    public Credentials()
    {
        this.pesel = "";
        this.password = "";
    }

    public Credentials(String pesel, String password)
    {
        this.pesel = pesel;
        this.password = password;
    }

    public boolean equals(java.lang.Object rhs)
    {
        if(this == rhs)
        {
            return true;
        }
        Credentials r = null;
        if(rhs instanceof Credentials)
        {
            r = (Credentials)rhs;
        }

        if(r != null)
        {
            if(this.pesel != r.pesel)
            {
                if(this.pesel == null || r.pesel == null || !this.pesel.equals(r.pesel))
                {
                    return false;
                }
            }
            if(this.password != r.password)
            {
                if(this.password == null || r.password == null || !this.password.equals(r.password))
                {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    public int hashCode()
    {
        int h_ = 5381;
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, "::Bank::Credentials");
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, pesel);
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, password);
        return h_;
    }

    public Credentials clone()
    {
        Credentials c = null;
        try
        {
            c = (Credentials)super.clone();
        }
        catch(CloneNotSupportedException ex)
        {
            assert false; // impossible
        }
        return c;
    }

    public void ice_writeMembers(com.zeroc.Ice.OutputStream ostr)
    {
        ostr.writeString(this.pesel);
        ostr.writeString(this.password);
    }

    public void ice_readMembers(com.zeroc.Ice.InputStream istr)
    {
        this.pesel = istr.readString();
        this.password = istr.readString();
    }

    static public void ice_write(com.zeroc.Ice.OutputStream ostr, Credentials v)
    {
        if(v == null)
        {
            _nullMarshalValue.ice_writeMembers(ostr);
        }
        else
        {
            v.ice_writeMembers(ostr);
        }
    }

    static public Credentials ice_read(com.zeroc.Ice.InputStream istr)
    {
        Credentials v = new Credentials();
        v.ice_readMembers(istr);
        return v;
    }

    static public void ice_write(com.zeroc.Ice.OutputStream ostr, int tag, java.util.Optional<Credentials> v)
    {
        if(v != null && v.isPresent())
        {
            ice_write(ostr, tag, v.get());
        }
    }

    static public void ice_write(com.zeroc.Ice.OutputStream ostr, int tag, Credentials v)
    {
        if(ostr.writeOptional(tag, com.zeroc.Ice.OptionalFormat.FSize))
        {
            int pos = ostr.startSize();
            ice_write(ostr, v);
            ostr.endSize(pos);
        }
    }

    static public java.util.Optional<Credentials> ice_read(com.zeroc.Ice.InputStream istr, int tag)
    {
        if(istr.readOptional(tag, com.zeroc.Ice.OptionalFormat.FSize))
        {
            istr.skip(4);
            return java.util.Optional.of(Credentials.ice_read(istr));
        }
        else
        {
            return java.util.Optional.empty();
        }
    }

    private static final Credentials _nullMarshalValue = new Credentials();

    /** @hidden */
    public static final long serialVersionUID = 8092506850638304773L;
}
