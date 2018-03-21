import React from 'react';
import ImagesWrapper from './SC/ImagesWrapper'

const Images = ({ imagesCount, selectedSet, handleImageClick, clickedCats }) => {
    const images = [...Array(imagesCount)].map(a => a).map((_, index) => {
        return (
            <img
                src={`http://localhost:8080/api/${selectedSet}/${index}`} alt=""
                onClick={handleImageClick.bind(null, index)}
                className={clickedCats.hasOwnProperty(index) ? clickedCats[index] === 1 ? "cat ": "no-cat" : "unclicked" }
                key={index}
            />
        )
    })

    return (
        <ImagesWrapper>{images}</ImagesWrapper>
    );
};

export default Images;