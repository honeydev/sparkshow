module Route exposing (..)

import Url exposing (Url)
import Url.Parser exposing (..)


type Route
    = Login
    | Default


parseUrl url =
    case parse matchRoute url of
        Just route ->
            route
        Nothing ->
            Default

matchRoute : Parser (Route -> a) a
matchRoute =
    oneOf
        [ map Default top
        , map Login (s "login")
        ]
