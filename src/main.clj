(ns main
  (:require
   [datomic.api :as d]
   [schema-voyager.cli :as svc]))

(def db-uri "datomic:dev://localhost:4334/compus-flow")

#_(d/delete-database db-uri)
(d/create-database db-uri)

(def conn (d/connect db-uri))

(def event
  [{:db/ident       :event/id
    :db/valueType   :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity}

   {:db/ident       :event/name
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Event name."}

   {:db/ident       :event/description
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Event description."}

   {:db/ident       :event/start-date
    :db/valueType   :db.type/instant
    :db/cardinality :db.cardinality/one
    :db/doc         "Event start date."}

   {:db/ident       :event/end-date
    :db/valueType   :db.type/instant
    :db/cardinality :db.cardinality/one
    :db/doc         "Event end date."}

   {:db/ident       :event/estimated-attendance
    :db/valueType   :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc         "Estimated attendance at the event."}

   {:db/ident       :event/organizations
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/doc         "Event organization."
    :db.schema/references [#schema/agg :organization]}

   {:db/ident       :event/venues
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/doc         "Event venues."
    :db.schema/references [#schema/agg :venue]}

   {:db/ident       :event/stock
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/doc         "Event stock of items."
    :db.schema/references [#schema/agg :stock]}

   {:db/ident       :event/attractions
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/doc         "Event attraction."
    :db.schema/references [#schema/agg :attraction]}

   {:db/ident       :event/batches
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/doc         "Valid ticket batches for the event."
    :db.schema/references [#schema/agg :batch]}

   {:db/ident       :event/participants
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/doc         "Event participants"
    :db.schema/references [#schema/agg :participant]}

   {:db/ident       :event/categories
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/doc         "Event categories, e.g., :event.category/party."
    :db.schema/references [#schema/enum :event.category]}

   {:db/ident :event.category/party}
   {:db/ident :event.category/cultural}
   {:db/ident :event.category/conference}
   {:db/ident :event.category/career}
   {:db/ident :event.category/social}])

(def organization
  [{:db/ident       :organization/id
    :db/valueType   :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity}

   {:db/ident       :organization/name
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Organization name."}

   {:db/ident       :organization/cnpj
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity
    :db/doc         "Organization CNPJ."}

   {:db/ident       :organization/description
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Organization description."}

   {:db/ident             :organization/categories
    :db/valueType         :db.type/ref
    :db/cardinality       :db.cardinality/many
    :db/doc               "Organization categories, e.g., :organization.category/athletic."
    :db.schema/references [#schema/enum :organization.category]}

   {:db/ident :organization.category/student-union}
   {:db/ident :organization.category/athletic}
   {:db/ident :organization.category/studies}
   {:db/ident :organization.category/research}])

(def venue
  [{:db/ident       :venue/id
    :db/valueType   :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity}

   {:db/ident       :venue/name
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Venue name."}

   {:db/ident       :venue/description
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Venue description."}

   {:db/ident       :venue/capacity
    :db/valueType   :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc         "Venue capacity."}

   {:db/ident       :venue/address
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Venue address."}])

(def money
  [{:db/ident       :money/value
    :db/valueType   :db.type/bigdec
    :db/cardinality :db.cardinality/one
    :db/doc         "Money value."}

   {:db/ident             :money/currency
    :db/valueType         :db.type/ref
    :db/cardinality       :db.cardinality/one
    :db/doc               "Money currency, e.g, :money.currency/BRL."
    :db.schema/references [#schema/enum :money.currency]}

   {:db/ident :money.currency/BRL
    :db/doc   "Brazilian Real."}
   {:db/ident :money.currency/USD
    :db/doc   "United States Dollar."}
   {:db/ident :money.currency/EUR
    :db/doc   "Euro."}])

(def stock
  [{:db/ident       :stock/id
    :db/valueType   :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity}

   {:db/ident       :stock/quantity
    :db/valueType   :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc         "Item quantity."}

   {:db/ident             :stock/spent
    :db/valueType         :db.type/ref
    :db/cardinality       :db.cardinality/one
    :db/doc               "Total spent on this item."
    :db.schema/references [#schema/agg :money]}

   {:db/ident             :stock/profit
    :db/valueType         :db.type/ref
    :db/cardinality       :db.cardinality/one
    :db/doc               "Total profit on this item."
    :db.schema/references [#schema/agg :money]}

   {:db/ident             :stock/item
    :db/valueType         :db.type/ref
    :db/cardinality       :db.cardinality/one
    :db/doc               "Item in this stock."
    :db.schema/references [#schema/agg :item]}])

(def item
  [{:db/ident       :item/id
    :db/valueType   :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity}

   {:db/ident       :item/name
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Item name."}

   {:db/ident             :item/purchase-price
    :db/valueType         :db.type/ref
    :db/cardinality       :db.cardinality/one
    :db/doc               "Item purchase price."
    :db.schema/references [#schema/agg :money]}

   {:db/ident             :item/sale-price
    :db/valueType         :db.type/ref
    :db/cardinality       :db.cardinality/one
    :db/doc               "Item sale price."
    :db.schema/references [#schema/agg :money]}

   {:db/ident             :item/product
    :db/valueType         :db.type/ref
    :db/cardinality       :db.cardinality/many
    :db/doc               "Item extra data, e.g., :shirt/size M"
    :db.schema/references [#schema/agg :product]}])

(def product
  [{:db/ident       :product/id
    :db/valueType   :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity}

   {:db/ident             :product/suppliers
    :db/valueType         :db.type/ref
    :db/cardinality       :db.cardinality/many
    :db/doc               "Product suppliers."
    :db.schema/references [#schema/agg :supplier]}

   {:db/ident       :product/type
    :db/valueType   :db.type/keyword
    :db/cardinality :db.cardinality/many
    :db/doc         "Product type."}

   ;; This should be used alongside with :product/type
   {:db/ident             :product/extra-data
    :db/valueType         :db.type/ref
    :db/cardinality       :db.cardinality/many
    :db/doc               "Product extra data."
    :db.schema/references [#schema/agg :shirt
                           #schema/agg :cup
                           #schema/agg :sticker
                           #schema/agg :other]}

   ;; Added on demmand
   {:db/ident       :shirt/size
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one}

   {:db/ident       :shirt/color
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one}

   {:db/ident       :cup/size
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one}

   {:db/ident       :cup/color
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one}

   {:db/ident       :sticker/type
    :db/valueType   :db.type/keyword
    :db/cardinality :db.cardinality/one}

   {:db/ident       :other/data
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/one}])

(def supplier
  [{:db/ident       :supplier/id
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/uuid
    :db/unique      :db.unique/identity}

   {:db/ident       :supplier/name
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/string
    :db/doc         "Supplier name."}

   {:db/ident       :supplier/cnpj
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/string
    :db/unique      :db.unique/identity
    :db/doc         "Supplier CNPJ."}

   {:db/ident       :supplier/address
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/string
    :db/doc         "Supplier address."}

   {:db/ident       :supplier/phone
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/string
    :db/doc         "Supplier phone number."}

   {:db/ident       :supplier/email
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/string
    :db/unique      :db.unique/identity
    :db/doc         "Supplier email."}])

(def feedback
  [{:db/ident       :feedback/id
    :db/valueType   :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity}

   {:db/ident       :feedback/score
    :db/valueType   :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc         "Feedback score."}

   {:db/ident       :feedback/comment
    :db/valueType   :db.type/string ; It probably should be a ref to a comment
    :db/cardinality :db.cardinality/one
    :db/doc         "Feedback comment."}])

(def attraction
  [{:db/ident       :attraction/id
    :db/valueType   :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity}

   {:db/ident       :attraction/name
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Attraction name."}

   {:db/ident       :attraction/description
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Attraction description."}

   {:db/ident             :attraction/score
    :db/valueType         :db.type/long
    :db/cardinality       :db.cardinality/one
    :db/doc               "Attraction total score."}

   {:db/ident             :attraction/feedback
    :db/valueType         :db.type/ref
    :db/cardinality       :db.cardinality/many
    :db/doc               "Attraction feedback."
    :db.schema/references [#schema/agg :feedback]}

   {:db/ident             :attraction/categories
    :db/valueType         :db.type/ref
    :db/cardinality       :db.cardinality/many
    :db/doc               "Attraction categories."
    :db.schema/references [#schema/enum :attraction.category]}

   {:db/ident :attraction.category/talk}
   {:db/ident :attraction.category/movie}
   {:db/ident :attraction.category/music}])

(def batch
  [{:db/ident       :batch/id
    :db/valueType   :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity}

   {:db/ident       :batch/opening-date
    :db/valueType   :db.type/instant
    :db/cardinality :db.cardinality/one
    :db/doc         "Batch opening date."}

   {:db/ident       :batch/closing-date
    :db/valueType   :db.type/instant
    :db/cardinality :db.cardinality/one
    :db/doc         "Batch closing date."}

   {:db/ident       :batch/active?
    :db/valueType   :db.type/boolean
    :db/cardinality :db.cardinality/one
    :db/doc         "Is batch active?"}

   {:db/ident       :batch/opening-type
    :db/valueType   :db.type/keyword
    :db/cardinality :db.cardinality/many
    :db/doc         "Batch opening type."}

   {:db/ident       :batch/name
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Batch name."}

   {:db/ident             :batch/price
    :db/valueType         :db.type/ref
    :db/cardinality       :db.cardinality/one
    :db/doc               "Batch price."
    :db.schema/references [#schema/agg :money]}

   {:db/ident       :batch/tickets
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/doc         "Batch tickets."
    :db.schema/references [#schema/agg :ticket]}])

(def ticket
  [{:db/ident       :ticket/id
    :db/valueType   :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity}

   {:db/ident       :ticket/purchase-date
    :db/valueType   :db.type/instant
    :db/cardinality :db.cardinality/one}])

(def order
  [{:db/ident       :order/id
    :db/valueType   :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity}

   {:db/ident       :order/date
    :db/valueType   :db.type/instant
    :db/cardinality :db.cardinality/one
    :db/doc         "Order date."}

   {:db/ident       :order/quantity
    :db/valueType   :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc         "Quantity of products purchased."}

   {:db/ident             :order/price
    :db/valueType         :db.type/ref
    :db/cardinality       :db.cardinality/one
    :db/doc               "Order price."
    :db.schema/references [#schema/agg :money]}

   {:db/ident       :order/withdrawn?
    :db/valueType   :db.type/boolean
    :db/cardinality :db.cardinality/one
    :db/doc         "If the order has been withdrawn."}

   {:db/ident :order/participant
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "Participant who made the order."
    :db.schema/references [#schema/agg :participant]}])

(def participant
  [{:db/ident       :participant/id
    :db/valueType   :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity}

   {:db/ident       :participant/cpf
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity
    :db/doc         "Participant CPF."}

   {:db/ident       :participant/name
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Participant name."}

   {:db/ident       :participant/email
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity
    :db/doc         "Participant email."}

   {:db/ident       :participant/birth-date
    :db/valueType   :db.type/instant
    :db/cardinality :db.cardinality/one
    :db/doc         "Participant birth date."}

   {:db/ident :participant/address
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "Participant address."}])

(def schema
  (vec (concat event
               organization
               venue
               money
               stock
               item
               product
               feedback
               attraction
               batch
               ticket
               supplier
               order
               participant)))

(schema-voyager.cli/standalone {:sources [{:static/data schema}]})

@(->> schema
      (map #(dissoc % :db.schema/references))
      (d/transact conn))
