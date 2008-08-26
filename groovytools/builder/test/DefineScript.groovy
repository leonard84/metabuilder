/**
 * invoice schema for ScriptTest
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
