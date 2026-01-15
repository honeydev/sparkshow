module Components.Navbar exposing (..)

import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick)
import Msg exposing (Msg(..))


type alias Path =
    String


type alias Name =
    String


type Link
    = Link Name Path


type NavButton
    = NavButton Name


linkToHtml : Link -> Html msg
linkToHtml link =
    case link of
        Link name path ->
            li [ class "nav-item" ] [ a [ class "nav-link", href path ] [ text name ] ]


buildLogIn : List Link -> Html Msg
buildLogIn links =
    let
        navLinks =
            List.map linkToHtml links
    in
    nav [ class "navbar navbar-light bg-light" ]
        [ navLinks |> ul [ class "navbar-nav" ]
        ]


buildSignOut : List Link -> NavButton -> Html Msg
buildSignOut links signOut =
    let
        navLinks =
            List.map linkToHtml links

        logOutLink =
            let
                signOutName =
                    case signOut of
                        NavButton name ->
                            name
            in
            span
                [ onClick SignOut ]
                [ text signOutName ]

        allLinks =
            navLinks ++ [ logOutLink ]
    in
    nav [ class "navbar navbar-light bg-light" ]
        [ allLinks |> ul [ class "navbar-nav" ]
        ]
