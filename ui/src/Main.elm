module Main exposing (..)

import Browser
import Browser.Navigation as Nav
import Components.Navbar as Navbar
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick)
import IndexPage exposing (..)
import Login as LoginPage
import Msg exposing (..)
import NotFound exposing (..)
import Platform.Cmd as Cmd
import Ports
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
    , session : Session
    }


getSession : Model -> Session
getSession model =
    pageSession model.pageModel


pageSession : Page -> Session
pageSession page =
    case page of
        IndexPage m ->
            m.session

        NotFoundPage ->
            Unauthenticated

        LoginPage m ->
            m.session


init : Maybe String -> Url.Url -> Nav.Key -> ( Model, Cmd Msg )
init flags url key =
    let
        initialSession =
            Session.sessionFromRawString flags

        route =
            Route.parseUrl url

        ( page, session ) =
            pageForRoute route initialSession
    in
    ( { pageModel = page, route = route, navKey = key, session = session }
    , Cmd.none
    )


pageForRoute : Route -> Session -> ( Page, Session )
pageForRoute route session =
    case route of
        Route.Index ->
            ( IndexPage { session = session }, session )

        Route.NotFound ->
            ( NotFoundPage, session )

        -- redirect on login page for (sign out and click on login link)
        _ ->
            ( LoginPage.init Unauthenticated |> LoginPage, session )


pageWithSession : Session -> Page -> Page
pageWithSession session page =
    case page of
        IndexPage m ->
            IndexPage { m | session = session }

        LoginPage m ->
            LoginPage { m | session = session }

        NotFoundPage ->
            NotFoundPage



-- UPDATE


authHook : a -> Page -> a
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

                ( page, session ) =
                    pageForRoute route model.session
            in
            ( { pageModel = page, route = route, navKey = model.navKey, session = session }, Cmd.none )

        LinkClicked urlRequest ->
            case urlRequest of
                Browser.Internal url ->
                    ( model, Nav.pushUrl model.navKey (Url.toString url) )

                Browser.External url ->
                    ( model, Nav.load url )

        LoginMessage subMsg ->
            let
                loginPageModel =
                    case model.pageModel of
                        LoginPage lpm ->
                            lpm

                        _ ->
                            LoginPage.init (getSession model)

                ( m, cmd ) =
                    LoginPage.update subMsg loginPageModel

                newPageModel =
                    LoginPage m

                newSession =
                    pageSession newPageModel
            in
            ( { pageModel = newPageModel, route = Route.Login, navKey = model.navKey, session = newSession }
            , Cmd.map LoginMessage cmd
            )

        SessionLoaded encoded ->
            let
                newSession =
                    Session.sessionFromRawString <| Just encoded

                updatedPage =
                    pageWithSession newSession model.pageModel
            in
            ( { model | pageModel = updatedPage, session = newSession }, Cmd.none )

        SignOut ->
            ( { model | session = Unauthenticated }, Cmd.batch [ Ports.removeLocalStorageItem "session", Nav.pushUrl model.navKey "/login" ] )



-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions _ =
    Ports.loadSession SessionLoaded



-- VIEW


view : Model -> Browser.Document Msg
view model =
    { title = "Sparkshow"
    , body =
        let
            mainNavElements =
                [ Navbar.Link "Queries" "queries", Navbar.Link "Profile" "profile" ]
        in
        [ ul []
            [ case model.session of
                Active _ ->
                    Navbar.buildSignOut mainNavElements (Navbar.NavButton "Sign Out")

                Unauthenticated ->
                    mainNavElements
                        ++ [ Navbar.Link "Login" "login" ]
                        |> Navbar.buildLogIn
            ]
        , div [] [ text <| Debug.toString model.session ]
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
