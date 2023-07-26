module Main exposing (Model, Msg(..), init, main, subscriptions, update, view, viewLink)

import Browser
import Browser.Navigation as Nav
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick)
import Json.Encode as Encode exposing (Value)
import Login
import Session exposing (..)
import Url



-- MAIN


main : Program (Maybe String) Model Msg
main =
    Browser.application
        { init = init
        , view = view
        , update = update
        , subscriptions = subscriptions
        , onUrlChange = UrlChanged
        , onUrlRequest = LinkClicked
        }



-- MODEL


type Model
    = Default Session
    | Login Login.Model


getSession : Model -> Session
getSession model =
    case model of
        Default s ->
            s

        Login m ->
            m.session


init : Maybe String -> Url.Url -> Nav.Key -> ( Model, Cmd Msg )
init flags url key =
    let
        store =
            storeSession { token = "abc" }
    in
    ( Default Unauthenticated
    , store
    )



-- UPDATE


type Msg
    = LinkClicked Browser.UrlRequest
    | UrlChanged Url.Url
    | GotLoginMessage Login.Msg


authHook msg model =
    case model of
        Default session ->
            case session of
                Unauthenticated ->
                    GotLoginMessage Login.PageOpened

                _ ->
                    msg

        Login subpageModel ->
            msg


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    let
        hookedMsg =
            authHook msg model

        _ =
            Debug.toString hookedMsg |> Debug.log "Hooked"
    in
    case hookedMsg of
        GotLoginMessage subMsg ->
            let
                ( m, cmd ) =
                    Login.update subMsg (Login.init (getSession model))
            in
            ( Login m, Cmd.map GotLoginMessage cmd )

        _ ->
            ( Default Unauthenticated, Cmd.none )



-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions _ =
    Sub.none



-- VIEW


view : Model -> Browser.Document Msg
view model =
    { title = "URL Interceptor"
    , body =
        [ text "The current URL is: "
        , ul []
            [ button [ onClick (GotLoginMessage Login.PageOpened) ] [ text "login" ]
            ]
        , case model of
            Login loginModel ->
                Html.map GotLoginMessage (Login.view loginModel)

            Default s ->
                div [] [ text "Default" ]
        ]
    }


viewLink : String -> Html msg
viewLink path =
    li [] [ a [ href path ] [ text path ] ]
