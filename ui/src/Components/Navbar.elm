module Components.Navbar exposing (Link(..), build)

import Html exposing (..)
import Html.Attributes exposing (..)


type alias Path =
    String


type alias Name =
    String


type Link
    = Link Name Path


linkToHtml : Link -> Html msg
linkToHtml link =
    case link of
        Link name path ->
            li [ class "list-group" ] [ a [ href path ] [ text name ] ]


build : List Link -> Html msg
build elements =
    nav [ class "nav" ]
        [ List.map linkToHtml elements |> ul [ class "list-group" ]
        ]
