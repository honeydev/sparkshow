module Login exposing (..)

import Browser.Navigation as Nav
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import Http
import Json.Decode as Decode
import Json.Encode as Encode
import Maybe
import Session exposing (..)
import Vars exposing (serverUrl)


type alias Model =
    { username : String
    , password : String
    , session : Session
    , message : Maybe String
    }


type alias Form =
    { username : String
    , password : String
    }


type alias LoginSuccess =
    { user : Session.User
    , token : String
    }


type alias LoginError =
    String


type Msg
    = ChangeUsername String
    | ChangePassword String
    | SendForm
    | PageOpened
    | LoginResult (Result LoginError LoginSuccess)


init : Session -> Model
init s =
    { username = ""
    , password = ""
    , session = s
    , message = Nothing
    }


sendForm : Form -> Cmd Msg
sendForm form =
    Http.request
        { method = "POST"
        , url = serverUrl ++ "/login"
        , body = Http.jsonBody (formEncoder form)
        , expect = Http.expectStringResponse LoginResult loginStringResponseDecoder
        , headers = []
        , timeout = Nothing
        , tracker = Nothing
        }


formEncoder : Form -> Decode.Value
formEncoder form =
    Encode.object
        [ ( "username", Encode.string form.username )
        , ( "password", Encode.string form.password )
        ]


loginSuccessDecoder : Decode.Decoder LoginSuccess
loginSuccessDecoder =
    Decode.map2 LoginSuccess
        (Decode.field "user" Session.userDecoder)
        (Decode.field "token" Decode.string)


loginStringResponseDecoder : Http.Response String -> Result LoginError LoginSuccess
loginStringResponseDecoder response =
    case response of
        Http.GoodStatus_ _ body ->
            case Decode.decodeString loginSuccessDecoder body of
                Ok success ->
                    Ok success

                Err decodeErr ->
                    Err (Decode.errorToString decodeErr)

        Http.BadStatus_ _ body ->
            case Decode.decodeString (Decode.field "message" Decode.string) body of
                Ok message ->
                    Err message

                Err _ ->
                    Err "Login failed"

        Http.BadUrl_ err ->
            Err err

        Http.Timeout_ ->
            Err "Request timed out"

        Http.NetworkError_ ->
            Err "Network error"


update : Msg -> Model -> Nav.Key -> ( Model, Cmd Msg )
update msg model navKey =
    case msg of
        ChangeUsername username ->
            ( { model | username = username }, Cmd.none )

        ChangePassword password ->
            ( { model | password = password }, Cmd.none )

        SendForm ->
            ( { model | message = Nothing }
            , sendForm { username = model.username, password = model.password }
            )

        PageOpened ->
            ( model, Cmd.none )

        LoginResult result ->
            case result of
                Ok success ->
                    ( { model
                        | session = Session.Active { token = success.token, user = success.user }
                      }
                    , Cmd.batch [ Session.storeSession { token = success.token, user = success.user }, Nav.pushUrl navKey "/queries" ]
                    )

                Err errMsg ->
                    ( { model | message = Just errMsg }, Cmd.none )


view : Model -> Html Msg
view model =
    div [ class "w-full max-w-md bg-white rounded-lg shadow-lg p-8" ]
        [ Maybe.withDefault (text "")
            (Maybe.map
                (\msg ->
                    div [ class "alert" ]
                        [ div [ class "border border-red-400 rounded bg-red-100 px-4 py-3 text-red-700" ] [ text msg ]
                        ]
                )
                model.message
            )
        , div [ class "p-4" ]
            [ label [ class "block text-sm font-medium text-gray-700 " ] [ text "Username" ]
            , input
                [ class "mt-1 w-full rounded border border-gray-300 focus:border-indigo-500 focus:ring-indigo-500"
                , value model.username
                , onInput ChangeUsername
                ]
                []
            ]
        , div
            [ class "p-4" ]
            [ label [ class "block text-sm font-medium text-gray-700" ] [ text "Password" ]
            , input
                [ class "mt-1 w-full rounded border border-gray-300 focus:border-indigo-500 focus:ring-indigo-500"
                , type_ "password"
                , value model.password
                , onInput ChangePassword
                ]
                []
            ]
        , button [ class "bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded", onClick SendForm ] [ text "Sign in" ]
        ]
