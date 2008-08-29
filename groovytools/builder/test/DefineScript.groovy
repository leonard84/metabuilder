/**
 * invoice schema for ScriptTest
 *
 * @author didge
 * @version $Id$
 */
invoice {
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
