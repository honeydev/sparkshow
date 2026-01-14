module Session exposing (..)

import Json.Decode as Decode
import Json.Encode as Encode
import Platform.Cmd exposing (Cmd)
import Ports
import Utils.Common exposing (..)


type Session
    = Unauthenticated
    | Active SessionData


type alias User =
    { id : Int
    , createdAt : String
    , updatedAt : String
    , username : String
    , email : Maybe String
    , passwordHash : String
    }


type alias SessionData =
    { token : String
    , user : User
    }


maybeEncodeString : Maybe String -> Encode.Value
maybeEncodeString maybeString =
    case maybeString of
        Just str ->
            Encode.string str

        Nothing ->
            Encode.null


userEncoder : User -> Encode.Value
userEncoder user =
    Encode.object
        [ ( "id", Encode.int user.id )
        , ( "createdAt", Encode.string user.createdAt )
        , ( "updatedAt", Encode.string user.updatedAt )
        , ( "username", Encode.string user.username )
        , ( "email", maybeEncodeString user.email )
        , ( "passwordHash", Encode.string user.passwordHash )
        ]


sessionEncoder : SessionData -> Encode.Value
sessionEncoder sessionData =
    Encode.object
        [ ( "token", Encode.string sessionData.token )
        , ( "user", userEncoder sessionData.user )
        ]


storeSession : SessionData -> Cmd msg
storeSession sessionData =
    sessionEncoder sessionData
        |> Encode.encode 0
        |> Ports.storeSession


userDecoder : Decode.Decoder User
userDecoder =
    Decode.map6 User
        (Decode.field "id" Decode.int)
        (Decode.field "createdAt" Decode.string)
        (Decode.field "updatedAt" Decode.string)
        (Decode.field "username" Decode.string)
        (Decode.field "email" (Decode.nullable Decode.string))
        (Decode.field "passwordHash" Decode.string)


sessionDecoder : Decode.Decoder SessionData
sessionDecoder =
    Decode.map2 SessionData
        (Decode.field "token" Decode.string)
        (Decode.field "user" userDecoder)


unpackSession : Result error SessionData -> Session
unpackSession packedSession =
    case packedSession of
        Ok v ->
            Active v

        Err e ->
            let
                _ =
                    "Parsing session data error: " ++ Debug.toString e |> Debug.log
            in
            Unauthenticated


sessionFromString string =
    Decode.decodeString sessionDecoder string
        |> Result.mapError anyValToStringErr


sessionFromRawString : Maybe String -> Session
sessionFromRawString rawSession =
    rawSession
        |> Result.fromMaybe (Err "Empty or invalid session")
        |> Result.andThen sessionFromString
        |> unpackSession
