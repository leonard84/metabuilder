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
        lines (min: 1, max: 4) {
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
