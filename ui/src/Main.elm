module Main exposing (..)

import Browser
import Browser.Navigation as Nav
import Components.Navbar as Navbar
import Html exposing (..)
import Html.Attributes exposing (..)
import IndexPage exposing (..)
import Login as LoginPage
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

        page =
            pageForRoute route initialSession
    in
    ( { pageModel = page, route = route, navKey = key, session = initialSession }
    , Cmd.none
    )


pageForRoute : Route -> Session -> Page
pageForRoute route session =
    case route of
        Route.Index ->
            IndexPage { session = session }

        Route.Login ->
            LoginPage.init session |> LoginPage

        Route.NotFound ->
            NotFoundPage


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


type Msg
    = LinkClicked Browser.UrlRequest
    | UrlChanged Url.Url
    | LoginMessage LoginPage.Msg
    | SessionLoaded String


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

                page =
                    pageForRoute route model.session
            in
            ( { pageModel = page, route = route, navKey = model.navKey, session = model.session }, Cmd.none )

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



-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions _ =
    Ports.loadSession SessionLoaded



-- VIEW


view : Model -> Browser.Document Msg
view model =
    { title = "Sparkshow"
    , body =
        [ ul []
            [ Navbar.build
                [ Navbar.Link "Queries" "queries"
                , Navbar.Link "Profile" "profile"
                , case model.session of
                    Active _ ->
                        Navbar.Link "Sign out" "sign-out"

                    Unauthenticated ->
                        Navbar.Link "Login" "login"
                ]
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
