(ns swagger-service.routes.services
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [ring.util.http-response :refer :all] ;; ECE 2017.08.08
            [clojure.java.io :as io] ;; ECE 2017.08.08
            [clojure.data.json :as json])) ;; ECE 2017.08.08

(import '[java.io StringWriter]
        '[java.net Socket URL])

;; ECE 2017.08.08
(defn send-request
  "Sends an HTTP GET request to the specified host, port, and path"
  [host port path]
  (with-open [sock (Socket. host port)
              writer (io/writer sock)
              reader (io/reader sock)
              response (StringWriter.)]
    (.append writer ( str path "\n"))
    (.flush writer)
    (io/copy reader response)
    (str response)))

;; ECE 2017.08.09 - https://github.com/metosin/compojure-api
(s/defschema Rectangle
  {:width s/Int
   :height s/Int})

(s/defschema Point
  {:x Long
   :y Long})

(s/defschema GamePiece
  {:id s/Str
   :name s/Str
   :imageName s/Str
   :stackName s/Str
   :dimension Rectangle
   :locationOld {:salvoMapId s/Str
                 :salvoPoint Point}
   :locationNew {:salvoMapId s/Str
                 :salvoPoint Point
                 }})

(s/defschema LeftDoubleClick
  {
   :targetType (s/enum :MAP :PALETTE)
   :targetId s/Str
   :salvoPoint Point})

(defapi service-routes
  {:swagger {:ui "/swagger-ui"
             :spec "/swagger.json"
             :data {:info {:version "1.0.0"
                           :title "Sample API"
                           :description "Sample Services"}}}}
  (context "/salvo" [] ;; ECE 2017.08.08
    :tags ["Vassal"] ;; ECE 2017.08.09

    (GET "/map" []
      :query-params [id :- s/Str]
      :summary      "Get the map with the id provided on the query parameter"
      (->
       (send-request "localhost" 3030  (str "{\"GET_SALVOMAP_BY_ID\": {\"queryParameters\": { \"id\": \"" id "\"}}}"))
       ok
       (content-type "application/json")))

    (GET "/gamestate" []
      :summary      "Get the complete game state"
      (->
       (send-request "localhost" 3030  (str "{\"GET_GAMESTATE\": {}}"))
       ok
       (content-type "application/json")))

    (GET "/toolbarmenu" []
      :summary      "Get the toolbar menu"
      (->
       (send-request "localhost" 3030  (str "{\"GET_TOOLBAR_MENU\": {}}"))
       ok
       (content-type "application/json")))

    (POST "/gamepiece" []
      :body-params [gamePiece :- GamePiece]
      :summary     "Move a GamePiece from locationOld to locationNew"
      (let [gamePieceStr (json/write-str gamePiece)]
        (->
          (send-request "localhost" 3030 (str "{\"POST_PIECE\": " gamePieceStr "}"))
         ok
         (content-type "application/json"))))

    (POST "/leftdoubleclick" []
      :body-params [leftDoubleClick :- LeftDoubleClick]
      :summary     "Left Double-Click the mouse on a target element"
      (let [leftDoubleClickStr (json/write-str leftDoubleClick)]
        (->
          (send-request "localhost" 3030 (str "{\"POST_LEFT_DOUBLE_CLICK\": " leftDoubleClickStr "}"))
         ok
         (content-type "application/json"))))

    (POST "/turntracker" []
      :summary     "Advance the turn-tracker"
      (->
       (send-request "localhost" 3030 (str "{\"POST_TURNTRACKER\": {}}"))
       ok
       (content-type "application/json"))))
  )
