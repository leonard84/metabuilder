import groovytools.builder.MetaBuilder

/**
 * <code>InvoiceExample</code> demonstrates a number of {@link MetaBuilder} features in an increasingly complicated
 * manner.
 *
 * @author didge
 */

MetaBuilder mb = new MetaBuilder()

mb.define {
    invoice1 {
        collections {
            items {
                item {
                    properties {
                        qty(req: true)
                    }
                }
            }
            payments {
                payment {
                    properties {
                        amt(req: true)
                    }
                }
            }
        }
    }
}

mb.build {
    invoice1 {
        items {
            item(qty: 1)
            item(qty: 20)
        }
        payments {
            payment(amt: 100.00)
        }
    }
}
