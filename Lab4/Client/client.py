import sys, Ice
import Bank.Bank_ice

with Ice.initialize(sys.argv) as communicator:
    base = communicator.stringToProxy("accountService/accountService:tcp -h localhost -p 10000:udp -h localhost -p 10000")

    obj = Bank.AccountPrx.checkedCast(base)
    
    if not obj:
        raise RuntimeError("Invalid proxy")

    print(obj.getAccountType())