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
            li [ class "nav-item" ] [ a [ class "nav-link", href path ] [ text name ] ]


build : List Link -> Html msg
build elements =
    nav [ class "navbar navbar-light bg-light" ]
        [ List.map linkToHtml elements |> ul [ class "navbar-nav" ]
        ]
