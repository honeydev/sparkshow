module Session exposing (..)

import Json.Decode as Decode
import Json.Encode as Encode
import Ports


type Session
    = Unauthenticated
    | Active SessionData


type alias SessionData =
    { token : String
    }


sessionEncoder : { a | token : String } -> Encode.Value
sessionEncoder sessionData =
    Encode.object
        [ ( "token", Encode.string sessionData.token )
        ]


sessionDecoder : Decode.Decoder SessionData
sessionDecoder =
    Decode.map SessionData (Decode.field "token" Decode.string)


extractSession : Maybe String -> Session
extractSession rawSession =
    case rawSession of
        Just v ->
            case Decode.decodeString sessionDecoder v of
                Ok sessionData ->
                    Active sessionData

                Err err ->
                    let
                        _ = Debug.toString err 
                            |> Debug.log "Cant parse session data:"
                    in
                    Unauthenticated

        Nothing ->
            Unauthenticated


storeSession : SessionData -> Cmd msg
storeSession sessionData =
    sessionEncoder sessionData
        |> Encode.encode 0
        |> Ports.storeSession
