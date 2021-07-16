#!/bin/bash

echo "Clojure"
clojure -X:test:example :dirs [\"example\"] :patterns [\"example.*\"]

echo "Node"
rm out/node-tests.js
./node_modules/.bin/shadow-cljs compile :test
node out/node-tests.js

echo "Browser"
rm out/karma-tests.js
./node_modules/.bin/shadow-cljs compile :browser-test
./node_modules/.bin/karma start --single-run


