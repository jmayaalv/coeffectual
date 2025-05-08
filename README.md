# Coeffectual

A lightweight, composable Clojure library for managing side effects and dependencies using the coeffect/effect pattern.

## Overview

Coeffectual provides a clean separation between your pure application logic and the side effects it produces. Inspired by patterns found in re-frame, this library helps you structure your Clojure applications with clear boundaries between:

- **Coeffects**: Values from the outside world that your application needs
- **Effects**: Actions your application wants to perform on the outside world

Think of coeffects as the inputs your functions need from external sources, and effects as the outputs your functions want to send to external systems.

## Installation

Add this to your deps.edn:

```clojure
{:deps {coeffectual/coeffectual   {:mvn/version "0.1.0"}}}
```

## Concepts

### Coeffects

Coeffects represent the process of gathering input from external sources. They are functions that take a context and return a value.

```clojure
;; Define a coeffect handler for getting the current time
(def get-current-time
  (fn [_context]
    (java.time.Instant/now)))

;; Use the coeffect in your system
(resolve-cofx! {:coeffects {}} {:time get-current-time})
;; => {:time #object[java.time.Instant 0x4c5f5fa6 "2025-05-08T10:30:15.123Z"]}
```

### Effects

Effects represent actions to be performed on the outside world. They are registered handlers that execute side effects in a controlled manner.

```clojure
;; Register an effect handler for HTTP requests
(register-fx! :http
  (fn [_context effect]
    (http-client/request effect)))

;; Execute effects
(execute-fx! context {:http {:method :get :url "https://example.com"}
                      :db {:op :insert :table :users :values {...}}})
```

## Key Features

- **Composable Coeffects**: Coeffects can access the context and other coeffects
- **Clean Separation**: Keep your core logic pure by isolating side effects
- **Testable**: Easy to test your application logic by mocking coeffects and effects

## Usage Example

```clojure
(ns my-app.core
  (:require [coeffectual.core :refer [resolve-cofx! execute-fx! register-fx!]]))

;; Register effect handlers
(register-fx! :http http-handler)
(register-fx! :db db-handler)

;; Define coeffect handlers
(def coeffects
  {:db (fn [_] (get-from-database))
   :user-id (fn [ctx] (get-in ctx [:request :params :user-id]))})

;; In your application flow
(defn process-request [request]
  (let [context {:request request :coeffects {}}
        ;; Gather all required inputs
        coeffects (resolve-cofx! context coeffects)
        ;; Process with your pure business logic
        result (my-business-logic coeffects)
        ;; Extract side effects to perform
        effects (:effects result)]
    ;; Execute all side effects in proper order
    (execute-fx! context effects)
    ;; Return the response
    (:response result)))
```

## Full Application Flow

Using the `execute!` function provides a complete flow:

```clojure
(ns my-app.handler
  (:require [coeffectual.core :as cf]))

(defn handle-user-registration
    ;;coeffects
    ([args]
     {:request-id (random-uuid)
      :user       (fn [{:keys [db]}]
                    (jdbc/query db ["select * From user where email = ?" (:email args)]))
      :now        (fn [_]
                    (java.time.Instant/now))})
    ;;business logic
    ([{:keys [request-id user now] :as coeffects} user-data]
     (when (nil? user)
       [user {:db    ["insert into user ( userid, email, date), values (?, ?, ?)"
                      request-id (:email user-data) now]
              :email {:to      (:email user-data)
                      :subject "Welcome!"
                      :body    "Thanks for registering."}}])))

;; resolve coeffects and run effects
(coeffectual/execute! {:db "jdbc:connection"} {:email "test@email.com"} handle-user-registration)
```

## Testing

The library is designed with testability in mind. You can easily test your logic by providing mock coeffects and verifying the effects that would be produced.

```clojure
(deftest test-my-logic
  (let [coeffects {:db mock-db :user "test-user"}
        result (my-business-logic coeffects args)]
    (is (= {:db {:op :update :entity 123}}
           (:effects result)))))
```

## Effect Execution Order

Effects are executed in a prioritized order defined in `effect.clj`. The default order is:

1. `:http` - Network requests
2. `:db` - Database operations
3. `:mq` - Message queue operations
4. `:csv` - CSV file operations
5. `:file` - File system operations
6. `:pdf` - PDF generation
7. `:notification` - System notifications
8. `:email` - Email sending
9. `:slack` - Slack messages
10. `:event` - Event dispatching

You can override this order for custom effect types as needed.

## Developer Notes

Thinking about coeffects and effects can be challenging at first. Here are some analogies that might help:

- **Coeffects are like ingredients**: Before cooking a meal, you need to gather all ingredients. Coeffects collect all the external inputs your function needs before processing.

- **Effects are like mailing letters**: You write the letters (pure logic), put them in envelopes with addresses (effects), and then the mail carrier (effect handlers) delivers them to their destinations.

- **The whole system is like a functional assembly line**: Raw materials come in (coeffects), get transformed (pure functions), and finished products go out (effects) - with each step clearly separated.

## License

Copyright © 2025

Released under the MIT License.
