module Session exposing (..)

import Json.Encode as Encode
import Ports


type Session
    = Unauthenticated
    | Active SessionData


type alias SessionData =
    { token : String
    }


sessionEncoder sessionData =
    Encode.object
        [ ( "token", Encode.string sessionData.token )
        ]


storeSession sessionData =
    sessionEncoder sessionData
        |> Encode.encode 0
        |> Ports.storeSession
