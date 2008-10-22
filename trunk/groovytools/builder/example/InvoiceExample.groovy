/**
 * <code>InvoiceExample</code> demonstrates a number of  {@link MetaBuilder}  features in an increasingly complicated
 * manner.
 *
 * @author didge
 * @version $Id$
 */

import groovytools.builder.*


MetaBuilder mb = new MetaBuilder()

//
// Basic invoice schema, no factories.  Output is a tree of Nodes.
//
def invoiceDef = mb.define {
    invoice {
        properties {
            date()
        }
        collections {
            items {
                item {
                    properties {
                        upc()
                        price()
                        qty()
                    }
                }
            }
            payments {
                payment {
                    properties {
                        amt()
                    }
                }
            }
        }
    }
}

def anInvoice = mb.build {
    invoice(date: new Date()) {
        items {
            item(upc: 123, qty: 1, price: 14.99)
            item(upc: 234, qty: 4, price: 14.99)
            item(upc: 345, qty: 6, price: 14.99)
        }
        payments {
            payment(amt: 10)
        }
    }
}

//
// Separating item and payment schemas from invoice's to demonstrate reusability.
// Numbering the schemas now to make later steps more clear.
// Adding def, req and check properties.
// The result is still a tree of Nodes.
//
def itemDef01 = mb.define {
    item01 {
        properties {
            upc(req: true)
            price(req: true, check: {it >= 0})
            qty(req: true)
        }
    }
}

def paymentDef01 = mb.define {
    payment01 {
        properties {
            amt(req: true)
        }
    }
}

def invoiceDef01 = mb.define {
    invoice01 {
        properties {
            date(def: new Date())
        }
        collections {
            items {
                item(schema: itemDef01)
            }
            payments {
                payment(schema: paymentDef01)
            }
        }
    }
}

def anInvoice01 = mb.build {
    invoice01 {
        items {
            item(upc: 123, qty: 1, price: 14.99)
            item(upc: 234, qty: 4, price: 14.99)
            item(upc: 345, qty: 6, price: 14.99)
        }
        payments {
            payment(amt: 10)
        }
    }
}

//
// Now create some classes to play with...
//
class Invoice {
    def date
    def items = []
    def payments = []
}

class Item {
    def upc
    def qty
    def price
}

class Payment {
    def amt
}

//
// Extending invoiceDef01.
// Adding factories, so real Invoices will be built.
// Note that previous attributes (req, def) are still used.
//
mb.define {
    invoice02(schema: invoiceDef01, factory: Invoice) {
        collections {
            items {
                item(schema: itemDef01, factory: Item)
            }
            payments {
                payment(schema: paymentDef01, factory: Payment)
            }
        }
    }
}

def anInvoice02 = mb.build {
    invoice02 {
        item(upc: 123, qty: 1, price: 14.99)
        item(upc: 234, qty: 4, price: 14.99)
        item(upc: 345, qty: 6, price: 14.99)
        payment(amt: 10)
    }
}

println(anInvoice02)
