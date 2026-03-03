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
            li [] [ a [ class "block px-4 py-2 rounded hover:bg-gray-700", href path ] [ text name ] ]


navBarLinks : List (Html msg) -> Html msg
navBarLinks links =
    nav [ class "navbar-custom p-4 space-y-2" ]
        links


unaunthenticatedNavbar : Link -> Html msg
unaunthenticatedNavbar logIn =
    [ linkToHtml logIn ] |> navBarLinks


authenticatedNavbar : List Link -> NavButton -> Html Msg
authenticatedNavbar links signOut =
    let
        commonNavbarLinks =
            List.map linkToHtml links

        logOutLink =
            let
                signOutName =
                    case signOut of
                        NavButton name ->
                            name
            in
            li
                [ class "list-none block px-4 py-2 rounded hover:bg-gray-700", onClick SignOut ]
                [ text signOutName ]

        allLinks =
            commonNavbarLinks ++ [ logOutLink ]
    in
    navBarLinks allLinks
