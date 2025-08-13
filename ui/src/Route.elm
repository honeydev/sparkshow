module Route exposing (..)

import Url exposing (Url)
import Url.Parser exposing (..)


type Route
    = Login
    | NotFound
    | Index


parseUrl : Url -> Route
parseUrl url =
    case parse matchRoute url of
        Just route ->
            route

        Nothing ->
            NotFound


matchRoute : Parser (Route -> a) a
matchRoute =
    oneOf
        [ map Index top
        , map Login (s "login")
        ]
