module Main exposing (..)

import Browser
import Browser.Navigation as Nav
import Components.Navbar as Navbar
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick)
import IndexPage exposing (..)
import Json.Encode as Encode exposing (Value)
import Login as LoginPage
import NotFound exposing (..)
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


type Page
    = IndexPage IndexPage.Model
    | LoginPage LoginPage.Model
    | NotFoundPage


type alias Model =
    { pageModel : Page
    , route : Route
    , navKey : Nav.Key
    }


getSession : Model -> Session
getSession model =
    case model.pageModel of
        IndexPage m ->
            m.session

        NotFoundPage ->
            Unauthenticated

        LoginPage m ->
            m.session


init : Maybe String -> Url.Url -> Nav.Key -> ( Model, Cmd Msg )
init flags url key =
    let
        -- FIXME: store session example, fix when impl login
       --  store =
       --      storeSession { token = "abc" } |> Debug.toString |> Debug.log


        session = extractSession flags

        _ =
            Debug.toString (extractSession flags) |> Debug.log "Flags"

        route =
            Route.parseUrl url

        page =
            case route of
                Route.Index ->
                    IndexPage { session = session }

                Route.Login ->
                    LoginPage.init session |> LoginPage

                Route.NotFound ->
                    NotFoundPage

        _ =
            Debug.toString page |> Debug.log "Page"
    in
    ( { pageModel = page, route = route, navKey = key }
    , Cmd.none
    )



-- UPDATE


type Msg
    = LinkClicked Browser.UrlRequest
    | UrlChanged Url.Url
    | LoginMessage LoginPage.Msg


authHook msg model =
    case model of
        IndexPage session ->
            case session of
                --Unauthenticated ->
                --    LoginMessage LoginPage.PageOpened
                _ ->
                    msg

        NotFoundPage ->
            msg

        LoginPage subpageModel ->
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
                route =
                    Route.parseUrl url

                page =
                    case route of
                        Route.Index ->
                            IndexPage { session = Unauthenticated }

                        Route.Login ->
                            LoginPage.init Unauthenticated |> LoginPage

                        Route.NotFound ->
                            NotFoundPage

                _ =
                    Debug.toString page |> Debug.log "Page"
            in
            ( { pageModel = page, route = route, navKey = model.navKey }, Cmd.none )

        LinkClicked urlRequest ->
            case urlRequest of
                Browser.Internal url ->
                    ( model, Nav.pushUrl model.navKey (Url.toString url) )

                Browser.External url ->
                    ( model, Nav.load url )

        LoginMessage subMsg ->
            let
                ( m, cmd ) =
                    LoginPage.update subMsg (LoginPage.init (getSession model))
            in
            ( { pageModel = LoginPage m, route = Route.Login, navKey = model.navKey }
            , Cmd.map LoginMessage cmd
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
        [ ul []
            [ Navbar.build
                [ Navbar.Link "Query" "query"
                , Navbar.Link "Profile" "profile"
                , Navbar.Link "Sign out" "sign-out"
                , Navbar.Link "Login" "login"
                ]
            ]
        , case model.pageModel of
            LoginPage m ->
                Html.map LoginMessage (LoginPage.view m)

            NotFoundPage ->
                NotFound.view

            IndexPage m ->
                IndexPage.view m
        ]
    }


viewLink : String -> Html msg
viewLink path =
    li [] [ a [ href path ] [ text path ] ]
