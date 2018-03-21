import styled from 'styled-components'

const SidebarWrapper = styled.div`
    height: 100vh;
    background-color: palevioletred;
    padding: 30px;
    flex-basis: 300px;
    flex:1;
    flex-direction:column;
    display:flex;
    align-content:center;
    justify-content: center;
    align-items: center;
    color: papayawhip;
    h1{
        text-transform: uppercase;
        font-weight:100;
        font-size:1.7rem;
    }
    .dataset{
        width: 300px;
        cursor: pointer;
        border: 5px solid;
        border-color: papayawhip;
        text-align:center;
        margin-bottom:10px;
        padding: 10px;
        h3{
            text-transform: uppercase;
            margin-top: .5rem;
        }
        p{
            font-size: 16px;
            margin:4px;
        }
        &.selected{
            border: 5px solid orange;
            background-color: rgba(235, 191, 79, .3)
        }
    }
`
export default SidebarWrapper