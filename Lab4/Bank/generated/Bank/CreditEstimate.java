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

public class CreditEstimate implements java.lang.Cloneable,
                                       java.io.Serializable
{
    public double nativeCurrency;

    public double foreignCurrency;

    public CreditEstimate()
    {
    }

    public CreditEstimate(double nativeCurrency, double foreignCurrency)
    {
        this.nativeCurrency = nativeCurrency;
        this.foreignCurrency = foreignCurrency;
    }

    public boolean equals(java.lang.Object rhs)
    {
        if(this == rhs)
        {
            return true;
        }
        CreditEstimate r = null;
        if(rhs instanceof CreditEstimate)
        {
            r = (CreditEstimate)rhs;
        }

        if(r != null)
        {
            if(this.nativeCurrency != r.nativeCurrency)
            {
                return false;
            }
            if(this.foreignCurrency != r.foreignCurrency)
            {
                return false;
            }

            return true;
        }

        return false;
    }

    public int hashCode()
    {
        int h_ = 5381;
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, "::Bank::CreditEstimate");
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, nativeCurrency);
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, foreignCurrency);
        return h_;
    }

    public CreditEstimate clone()
    {
        CreditEstimate c = null;
        try
        {
            c = (CreditEstimate)super.clone();
        }
        catch(CloneNotSupportedException ex)
        {
            assert false; // impossible
        }
        return c;
    }

    public void ice_writeMembers(com.zeroc.Ice.OutputStream ostr)
    {
        ostr.writeDouble(this.nativeCurrency);
        ostr.writeDouble(this.foreignCurrency);
    }

    public void ice_readMembers(com.zeroc.Ice.InputStream istr)
    {
        this.nativeCurrency = istr.readDouble();
        this.foreignCurrency = istr.readDouble();
    }

    static public void ice_write(com.zeroc.Ice.OutputStream ostr, CreditEstimate v)
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

    static public CreditEstimate ice_read(com.zeroc.Ice.InputStream istr)
    {
        CreditEstimate v = new CreditEstimate();
        v.ice_readMembers(istr);
        return v;
    }

    static public void ice_write(com.zeroc.Ice.OutputStream ostr, int tag, java.util.Optional<CreditEstimate> v)
    {
        if(v != null && v.isPresent())
        {
            ice_write(ostr, tag, v.get());
        }
    }

    static public void ice_write(com.zeroc.Ice.OutputStream ostr, int tag, CreditEstimate v)
    {
        if(ostr.writeOptional(tag, com.zeroc.Ice.OptionalFormat.VSize))
        {
            ostr.writeSize(16);
            ice_write(ostr, v);
        }
    }

    static public java.util.Optional<CreditEstimate> ice_read(com.zeroc.Ice.InputStream istr, int tag)
    {
        if(istr.readOptional(tag, com.zeroc.Ice.OptionalFormat.VSize))
        {
            istr.skipSize();
            return java.util.Optional.of(CreditEstimate.ice_read(istr));
        }
        else
        {
            return java.util.Optional.empty();
        }
    }

    private static final CreditEstimate _nullMarshalValue = new CreditEstimate();

    /** @hidden */
    public static final long serialVersionUID = 3004305523330494140L;
}
