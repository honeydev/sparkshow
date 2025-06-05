module Pages.Login exposing (..)

import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import Http
import Json.Decode exposing (Decoder, field, map2, string)
import Json.Encode as Encode
import Models.Login exposing (PageModel)
import Models.Model exposing (Model)

type Msg
    = ChangeUsername String
    | ChangePassword String
    | SendForm
    | PageOpened
    | GetResult (Result Http.Error Response)

type alias Response =
    { status : String
    , token : String
    }

encodeReq : PageModel -> Encode.Value
encodeReq model =
    Encode.object
            [ ( "username", Encode.string model.username )
            , ( "password", Encode.string model.password )
            ]


sendForm : PageModel -> Cmd Msg
sendForm model =
    Http.post
        { url = "localhost:8085/login"
        , body = Http.jsonBody <| encodeReq model
        , expect = Http.expectJson GetResult formDecoder
        }


formDecoder : Decoder Response
formDecoder =
    map2 Response
        (field "status" string)
        (field "token" string)


update : Msg -> Model -> PageModel -> ( PageModel, Cmd Msg )
update msg model pageModel =

       let
           _ = Debug.toString model |> Debug.log "Login action"
       in
    case msg of
        ChangeUsername username ->
            ( { pageModel | username = username }
            , Cmd.none
            )

        ChangePassword password ->
            ( { pageModel | password = password }
            , Cmd.none
            )

        SendForm ->
            ( pageModel, sendForm pageModel)

        PageOpened ->
            ( pageModel, Cmd.none )

        GetResult response ->
            ( pageModel, Cmd.none )


view model =
    div []
        [ label [] [ text "Username" ]
        , input [ value model.username, onInput ChangeUsername ] []
        , label [] [ text "Password" ]
        , input [ type_ "password", value model.password, onInput ChangePassword ] []
        , button [ onClick SendForm ] [ text <| Debug.toString model ]
        ]
