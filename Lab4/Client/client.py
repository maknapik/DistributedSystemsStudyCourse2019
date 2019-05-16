import sys, Ice
import Bank.Bank_ice


def serveAccountFactory():
    while True:
        line = input("==> ")

        if line == "create":
            createAccount()
        elif line == "login":
            login()
        else:
            print("Unrecognized command")


def createAccount():
    line = input("(name surname pesel balance) ==> ")
    parameters = line.split(" ")

    accountCreated = accountFactoryPrx.createAccount(parameters[0], parameters[1], parameters[2], float(parameters[3]))

    print("Save your password: ", accountCreated.password)
    print("Your account is: ", accountCreated.userAccountType)
    print("You can login now")


def login():
    line = input("(pesel password) ==> ")
    parameters = line.split(" ")

    try:
        accountPrx = accountFactoryPrx.obtainAccess(Bank.Credentials(parameters[0], parameters[1]))
    except Bank.InvalidCredentialsException:
        print("Invalid credentials!")
        return

    print("You are logged in.")
    serveAccount(accountPrx)


def credit(accountPrx):
    line = input("(currency, amount, period) ==> ")
    parameters = line.split(" ")

    currency = Bank.Currency.PLN
    if parameters[0] == "PLN":
        currency = Bank.Currency.PLN
    elif parameters[0] == Bank.Currency.USD:
        currency = Bank.Currency.CHF
    elif parameters[0] == Bank.Currency.CHF:
        currency = Bank.Currency.CHF
    elif parameters[0] == Bank.Currency.EUR:
        currency = Bank.Currency.EUR

    creditEstimate = accountPrx.applyForCredit(currency, float(parameters[1]), parameters[2])
    print("Available credit option: (native) ", creditEstimate.nativeCurrency, ", (foreign) ", creditEstimate.foreignCurrency)


def serveAccount(accountPrx):
    while True:
        line = input("==> ")

        if line == "account type":
            print("Your account type: ", accountPrx.getAccountType())
        elif line == "balance":
            print("Your balance: ", accountPrx.getAccountBalance())
        elif line == "credit":
            credit(accountPrx)
        elif line == "logout":
            print("Logged out.")
            serveAccountFactory()
        else:
            print("Unrecognized command")


with Ice.initialize(sys.argv) as communicator:
    line = input("(name category) ==> ")
    parameters = line.split(" ")
    base = communicator.stringToProxy(parameters[0] + "/" + parameters[1] + ":tcp -h localhost -p 10000:udp -h localhost -p 10000")

    accountFactoryPrx = Bank.AccountFactoryPrx.checkedCast(base)

    if not accountFactoryPrx:
        raise RuntimeError("Invalid proxy")

    accountPrx = None

    serveAccountFactory()
