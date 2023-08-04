module Main exposing (Model, Msg(..), init, main, subscriptions, update, view, viewLink)

import Browser
import Browser.Navigation as Nav
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick)
import Json.Encode as Encode exposing (Value)
import Login as LoginPage
import Route exposing (Route)
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

type Page = Default Session
    | Login LoginPage.Model

type alias Model = {
    pageModel: Page
    , route: Route
    , navKey: Nav.Key
    }



getSession : Model -> Session
getSession model =
    case model.pageModel of
        Default s ->
            s

        Login m ->
            m.session

init : Maybe String -> Url.Url -> Nav.Key -> ( Model, Cmd Msg )
init flags url key =
    let
        -- FIXME: remove this
        store =
            storeSession { token = "abc" }
        route = Route.parseUrl url
        page  = case route of
            Route.Default -> Default Unauthenticated
            Route.Login   -> LoginPage.init Unauthenticated |> Login
        _ = Debug.toString page |> Debug.log "Page"
    in
    ( { pageModel = page, route = route, navKey = key }
    , store
    )



-- UPDATE


type Msg
    = LinkClicked Browser.UrlRequest
    | UrlChanged Url.Url
    | LoginMessage LoginPage.Msg


authHook msg model =
    case model of
        Default session ->
            case session of
                --Unauthenticated ->
                --    LoginMessage LoginPage.PageOpened

                _ ->
                    msg

        Login subpageModel ->
            msg


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    let
        hookedMsg =
            authHook msg model.pageModel
        _ =
            Debug.toString hookedMsg |> Debug.log "Hooked"
    in
    case hookedMsg of
        UrlChanged url ->
            let
                    route = Route.parseUrl url
                    page  = case route of
                        Route.Default -> Default Unauthenticated
                        Route.Login   -> LoginPage.init Unauthenticated |> Login
                    _ = Debug.toString page |> Debug.log "Page"
            in ({ pageModel = page, route = route, navKey = model.navKey }, Cmd.none)
        LinkClicked urlRequest ->
            case urlRequest of
                Browser.Internal url -> ( model, Nav.pushUrl model.navKey (Url.toString url) )
                Browser.External url -> ( model, Nav.load url )


        LoginMessage subMsg ->
            let
                ( m, cmd ) =
                    LoginPage.update subMsg (LoginPage.init (getSession model))
            in
            (
            { pageModel = Login m, route = Route.Login, navKey = model.navKey },
            Cmd.map LoginMessage cmd
            )


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
            [ a [ href "login" ] [ button [] [ text "login" ] ]]
        , case model.pageModel of
            Login loginModel ->
                Html.map LoginMessage (LoginPage.view loginModel)

            Default s ->
                div [] [ text "Default" ]
        ]
    }


viewLink : String -> Html msg
viewLink path =
    li [] [ a [ href path ] [ text path ] ]
