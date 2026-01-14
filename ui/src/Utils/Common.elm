module Utils.Common exposing (..)


anyValToStringErr : a -> Result String value
anyValToStringErr v =
    Debug.toString v
        |> Err


debugAsString : a -> b -> b
debugAsString v =
    v |> Debug.toString |> Debug.log
