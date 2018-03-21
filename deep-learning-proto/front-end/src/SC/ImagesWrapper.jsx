import styled from 'styled-components'

const ImagesWrapper = styled.div`
    height: 100vh;
    width: auto;
    padding: 20px;
    overflow:scroll;
    h3{
        text-transform: uppercase;
    }
    img{
        &:hover{
            cursor: pointer;
            transform: scale(1.05);
        }
        padding: 5px;
        margin: 3px;
        height:200px;
        width:200px;
        &.cat{
            border: 5px solid green;
        }
        &.no-cat{
            border: 5px solid red;
        }
    }
`
export default ImagesWrapper