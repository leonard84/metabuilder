/**
 * invoice schema for ScriptTest
 *
 * @author didge
 * @version $Id$
 */
order (factory: Order) {
    properties {
        id(req: true)
    }
    collections {
        lines {
            line(factory: OrderLine) {
                properties {
                    upc(req: true)
                    qty(req: true)
                    price(req: true)
                }
            }
        }
    }
}
