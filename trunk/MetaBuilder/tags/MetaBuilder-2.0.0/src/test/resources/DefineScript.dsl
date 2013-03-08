/**
 * invoice schema for ScriptTest
 *
 * @author didge
 * @version $Id: DefineScript.groovy 73 2013-02-01 19:55:48Z damokles $
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
